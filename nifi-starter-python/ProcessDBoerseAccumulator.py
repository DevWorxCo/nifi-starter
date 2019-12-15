from Plotter import read_json_and_output_image

import sys

print("Arg 1 - ", sys.argv[1], type(sys.argv[1]))
print("Arg 2 - ", sys.argv[2], type(sys.argv[2]))

data = sys.stdin.readlines()

read_json_and_output_image(sys.argv[1], data, sys.argv[2])




