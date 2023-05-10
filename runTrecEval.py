# trec_eval executable have to me in ../trec_eval-9.0.7 folder

import subprocess

script1 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-01_en_en.txt"
script2 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-02_en_en_3gram.txt"
script3 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-03_en_en_4gram.txt"
script4 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-04_en_en_5gram.txt"
script5 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-05_en_en_fr_5gram.txt"
script6 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-06_en_en_4gram_ner.txt"
script7 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-07_fr_fr.txt"
script8 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-08_fr_fr_3gram.txt"
script9 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-09_fr_fr_4gram.txt"
script10 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-10_fr_fr_5gram.txt"
script11 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-11_fr_en_fr_5gram.txt"
script12 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-12_fr_fr_4gram_ner.txt"

# Clear maps score file
with open('runs/experiments/trec_eval/all_maps.txt', 'w') as f:
    pass

for i in range(1, 13):
    script = eval('script' + str(i))
    result = subprocess.run(script, shell=True, capture_output=True, text=True)
    with open('runs/experiments/trec_eval/output' + str(i) + '.txt', 'w') as f:
        f.write(result.stdout)
    # Get the first and 6th lines of the output
    first_line = result.stdout.split('\n')[0]
    sixth_line = result.stdout.split('\n')[5]

    # Save the line to a text file
    with open('runs/experiments/trec_eval/all_maps.txt', 'a') as f:
        f.write(f"{first_line}\n{sixth_line}\n")