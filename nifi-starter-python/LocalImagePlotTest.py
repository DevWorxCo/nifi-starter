
from Plotter import read_json_and_output_image

data = open('/tmp/DBoerseAccumulatorProcessor/DBoerseState.json').readlines()
read_json_and_output_image('2019-12-13',data, '/tmp/DBoerseAccumulatorProcessor/DBoerseState.png')




