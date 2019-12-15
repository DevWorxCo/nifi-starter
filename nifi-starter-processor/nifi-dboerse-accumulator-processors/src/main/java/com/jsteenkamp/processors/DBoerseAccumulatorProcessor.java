package com.jsteenkamp.processors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;

import com.jsteenkamp.processors.state.DBoerseState;
import com.jsteenkamp.processors.state.DBoerseStateService;


@Tags({"Deutsche Boerse", "Accumulator", "Xetra"})
@CapabilityDescription("This is an simple example Nifi Processor that is part of the Nifi starter project demonstration. "
		+ "It takes the freely available data from the excellent Deutsche Boerse (https://github.com/Deutsche-Boerse/dbg-pds) "
		+ "source and accumulates the CSV files into a JSON file that keeps the MaxPrice, MinPrice, TradedVolume, NumberOfTrades columns grouped by security and date")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class DBoerseAccumulatorProcessor extends AbstractProcessor {

	public static final PropertyDescriptor TEMPORARY_DIRECTORY_LOCATION = new PropertyDescriptor
            .Builder().name("TemporaryDirectoryLocation")
            .displayName("Temporary Directory Location to Update the Daily Accumulator File")
            .description("The directory holding the state for this aggreation.")
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .defaultValue("/tmp/DBoerseAccumulatorProcessor")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

	public static final Relationship FAILURE = new Relationship.Builder()
    		.name("failure")
            .description("These failures are encountered if the system is unable consume this particular item")
            .build();
    
    public static final Relationship SUCCESS = new Relationship.Builder()
    		.name("success")
            .description("If the system was able to successfully process this item.")
            .build();

    private final AtomicReference<DBoerseStateService> dbStateServiceRef;
    private final AtomicReference<DBoerseState> dbstateRef;
    
    public DBoerseAccumulatorProcessor()
    {
    	dbstateRef = new AtomicReference<>();
    	dbStateServiceRef = new AtomicReference<>();
    }
    
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) 
    {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(TEMPORARY_DIRECTORY_LOCATION);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(SUCCESS);
        relationships.add(FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

	@Override
	public Set<Relationship> getRelationships()
	{
		return this.relationships;
	}

	@Override
	public final List<PropertyDescriptor> getSupportedPropertyDescriptors()
	{
		return descriptors;
	}
	
	@OnScheduled
	public void onScheduled(final ProcessContext context)
	{
		final ComponentLog logger = getLogger();
		final String dir = context.getProperty(TEMPORARY_DIRECTORY_LOCATION).evaluateAttributeExpressions().getValue();

		logger.info("Attempting to Initialize on the Directory Param : " + dir);
		
		try
		{
			DBoerseStateService srv = new DBoerseStateService(dir);
			dbStateServiceRef.set(srv);
			dbstateRef.set(srv.getOrLoadDBoerseState());
		} 
		catch (Exception e)
		{
			throw new ProcessException("Unable to initialize the state from : " + dir, e);
		}
	}

	@Override
	public void onTrigger(	final ProcessContext context,
							final ProcessSession session)
			throws ProcessException
	{
		FlowFile flowFile = session.get();
		if (flowFile == null)
		{
			return;
		}
		
		try
		{
			//Get the contents of the CSV and pass it along...
			session.read(flowFile, new InputStreamCallback()
			{
				@Override
				public void process(InputStream in) throws IOException
				{
					dbStateServiceRef.get().updateDBoerseStateWithCSVStream(dbstateRef.get(), in);
				}
			});
			
			FlowFile newFlowFile = session.write(session.create(flowFile), new OutputStreamCallback() 
			{
				@Override
				public void process(OutputStream out) throws IOException
				{
					byte[] data = dbStateServiceRef.get().persistAndReturnDBoerseState(dbstateRef.get());
					out.write(data);
				}
			});
			
			session.remove(flowFile); //remove it so that we don't get the relationship problem !
			session.transfer(newFlowFile, SUCCESS);
		} 
		catch (Exception e)
		{
			final ComponentLog logger = getLogger();
			logger.error("Unable to process the files - got the exception : " + e, e);
			session.transfer(flowFile, FAILURE);
		}
		
	}
}



