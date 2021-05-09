# Apache Nifi Starter Project

![Build](https://github.com/DevWorxCo/nifi-starter/workflows/Build/badge.svg)

Apache Nifi is a fantastic product that massively boosts your productivity whenever you need to deal with any form of data loading or process automation.  

The purpose of this starter project is to serve as a template to quickly get someone up and running with Nifi and overcome that initial inertia associated in starting with a blank project canvas. This starter project, when you run the commands below, includes a flow that does the following:

* Fetches from AWS S3 the freely available [Deutsche Börse Public Dataset](https://github.com/Deutsche-Boerse/dbg-pds)
* Runs the CSV files through a custom Nifi Processor (nifi-dboerse-accumulator-nar - included as part of this project)
* Invokes a Python script that takes the JSON summary produced by the above step and plots a chart.

## Quick Start in 3 Steps

Assuming you have [Java 11](https://adoptopenjdk.net/), Maven (https://maven.apache.org/) and Python 3 (with matplotlib) installed, you should be able to get the cluster up and running by executing this following commands:

### Step (1)

```
mvn clean install
```

This will build all the binaries required for this installation. *Please Note* - this step will also download the Nifi Binary (which is quite big - around 1.4 GB !)

### Step (2)

```
mvn initialize -Pinstall-start-nifi
```

That will extract the Nifi binaries, deploy the Python scripts, initialise the default flow and start up Nifi. *Please Note* - you will need to ensure you have nothing running on port 8080 (as Nifi will attempt to listen on it).


### Step (3)

That's it - after waiting a few seconds (takes around 13 seconds on my machine - you can verify by tailing the `nifi-deployed/nifi-1.13.2/logs/nifi-app.log` file) you are now ready to open the browser and inspect the flow - http://localhost:8080/nifi/ :

 
![Alt text](Nifi-Flow-Screenshot.png?raw=true "Nifi Flow Screenshot")

By default all the processors are stopped, by you can easily do a 'Select All' action and click the "Play" icon on the "Operate" panel 


## Explanation of the Flow 

The Nifi flow is defined from left to right. The first processor - `List DBoerse XETRA For Specfic Date` - connects to the _deutsche-boerse-xetra-pds_ AWS S3 bucket and finds all the CSV files for a specific date. In this example we have hard coded the '2019-12-13' date parameter, though you can easily make this dynamic by using Nifi's built-in expression editor (for instance to get today's date, you can use something like `${now():format('yyyy-MM-dd') }`).

The file names in the flow eventually reaches the `FetchS3Object` which is fetches the actual CSV file from the S3 bucket and passes it along to our custom processor - `DBoerseAccumulatorProcessor`. If anything goes wrong, the flow file (and error) is redirected to the `Log Failure` processor (in fact, I reuse this processor for a number of error conditions). In a production system, you may want to consider this processor to raise an alert to your operations team rather than just putting content in the Nifi log file.

The `DBoerseAccumulatorProcessor` is a custom processor that aggregates up the Deutsche Börse and groups it by Business Date and ISIN. It does so by keeping a `com.jsteenkamp.processors.state.DBoerseState` object in memory which it then also periodically flushes down to disk (and reads from on start-up if say, Nifi crashes). The file is located here - `nifi-deployed/nifi-1.13.2/nifi-starter-workdir/DBoerseState.json` (by default, you can change this in the processor configuration) and looks a little bit like:

```javascript
{
  "accumulations": {
    "2019-12-13": {
      "date": "2019-12-13",
      "entries": {
        "GB00BVJF7G73": {
          "isin": "GB00BVJF7G73",
          "mnemonic": "CCMR",
          "securityDescription": "CO.CCBI RQFII MON.MK.A CH",
          "tradedVolume": 8,
          "numberOfTrades": 4,
          "maxPrice": 103.644,
          "minPrice": 102.766
        },
        "IE00BF4G7290": {
          "isin": "IE00BF4G7290",
          "mnemonic": "JPGM",
          "securityDescription": "JPM ICAV-MGD FUTS EFT DLA",
          "tradedVolume": 4,
          "numberOfTrades": 4,
          "maxPrice": 23.71,
          "minPrice": 23.55
        }
      }
    }
  }
}
```

The last action in the flow is the `ExecuteStreamCommand` processor that invokes Python in order to generate a chart from the `DBoerseState.json` file. It effectively invokes the  `nifi-starter-python/ProcessDBoerseAccumulator.py` passing in as arguments the business date and the location of the generated image file. The content of the Nifi flow file (the `DBoerseState.json` ) is passed along through the standard input stream. For instance:

```python
from Plotter import read_json_and_output_image
import sys
#...
data = sys.stdin.readlines()
#...
read_json_and_output_image(sys.argv[1], data, sys.argv[2])
```
  
The output image file (`nifi-deployed/nifi-1.13.2/nifi-starter-workdir/python-plot-from-nifi.png` by default) produced by the Python script looks something like this:

![Alt text](python-plot-from-nifi.png?raw=true "Output Plot From Python")
 
  
## Stopping Nifi

To stop Nifi you can run:
 
```
cd nifi-deployed/nifi-1.13.2/bin/
./nifi.sh stop
```




