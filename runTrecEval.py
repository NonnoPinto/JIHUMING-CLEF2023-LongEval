# trec_eval executable have to me in ../trec_eval-9.0.7 folder

import subprocess

script1 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-01_en_en.txt"
script2 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-02_en_en_3gram.txt"


result = subprocess.run(script, shell=True, capture_output=True, text=True)

# print(result.stdout)

# Save the output to a text file
with open('output.txt', 'w') as f:
    f.write(result.stdout)