import sys
import json

import matplotlib.pyplot as plt; plt.rcdefaults()
import numpy as np
import matplotlib.pyplot as plt
import operator




def read_json_and_output_image(date, data_lines, output_file_name):

    json_data = json.loads(data_lines[0])
    accum_map = json_data['accumulations']

    if not date in accum_map:
        return

    entry_data = accum_map[date]['entries']

    plot_values = {}

    for key in entry_data.keys():
        plot_values[entry_data[key]['mnemonic']] = entry_data[key]['tradedVolume']

    plot_values_sorted = dict(sorted(plot_values.items(), key=operator.itemgetter(1), reverse=True))

    mnemonics = []
    traded_volumes = []

    count = 0
    for key in plot_values_sorted.keys():
        print(key, plot_values_sorted[key])
        mnemonics.append(key)
        traded_volumes.append(plot_values_sorted[key])
        count = count + 1
        if count > 10:
            break

    print(mnemonics)
    print(traded_volumes)

    y_pos = np.arange(len(mnemonics))
    plt.bar(y_pos, traded_volumes, align='center', alpha=0.5)
    plt.xticks(y_pos, mnemonics)
    plt.ylabel('Trading Volume')
    plt.title('Top 10 Trading Volume on DBoerse')

    # plt.show()

    plt.savefig(output_file_name)


