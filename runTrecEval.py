# This pip script runs trec_eval all in all 12 experiments and save the output to a text file
# The output of the trec_eval is saved in runs/experiments/scores folder
# It also saves, for each run, map score in runs/experiments/scores/all_maps.txt

# trec_eval executable have to me in ../trec_eval-9.0.7 folder

import subprocess

# Chose trec eval script to run with above parameters
# 1 = all runs
# 2 = short term runs
# 3 = long term runs
# 4 = held out test
run = 1

if run == 1:
    # trec_eval all_trec commands
    # script1 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-01_en_en.txt"
    # script2 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-02_en_en_3gram.txt"
    # script3 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-03_en_en_4gram.txt"
    # script4 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-04_en_en_5gram.txt"
    # script5 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-05_en_en_fr_5gram.txt"
    # script6 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-06_en_en_4gram_ner.txt"
    script1 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-07_fr_fr.txt"
    script2 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-08_fr_fr_3gram.txt"
    script3 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-09_fr_fr_4gram.txt"
    script4 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-10_fr_fr_5gram.txt"
    # script11 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-11_fr_en_fr_5gram.txt"
    script5 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/qrels.txt runs/experiments/seupd2223-JIHUMING-12_fr_fr_4gram_ner.txt"
    total_script = 5 # 12 when all scripts uncommented
    outFileName = "runs/experiments/scores/all_scores"
    scoretype = "all"
if run == 2:
    # trec_eval all_trec commands - SHORT TERM
    script1 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/a-short-july.txt runs/experiments/seupd2223-JIHUMING-07_fr_fr/seupd2223-JIHUMING-07_fr_fr.ST"
    script2 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/a-short-july.txt runs/experiments/seupd2223-JIHUMING-08_fr_fr_3gram/seupd2223-JIHUMING-08_fr_fr_3gram.ST"
    script3 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/a-short-july.txt runs/experiments/seupd2223-JIHUMING-09_fr_fr_4gram/seupd2223-JIHUMING-09_fr_fr_4gram.ST"
    script4 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/a-short-july.txt runs/experiments/seupd2223-JIHUMING-10_fr_fr_5gram/seupd2223-JIHUMING-10_fr_fr_5gram.ST"
    script5 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/a-short-july.txt runs/experiments/seupd2223-JIHUMING-12_fr_fr_4gram_ner/seupd2223-JIHUMING-12_fr_fr_4gram_ner.ST"
    total_script = 5
    outFileName = "runs/experiments/scores/ST_scores"
    scoretype = "ST"
if run == 3:
    # trec_eval all_trec commands - LONG TERM
    script1 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/b-long-september.txt runs/experiments/seupd2223-JIHUMING-07_fr_fr/seupd2223-JIHUMING-07_fr_fr.LT"
    script2 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/b-long-september.txt runs/experiments/seupd2223-JIHUMING-08_fr_fr_3gram/seupd2223-JIHUMING-08_fr_fr_3gram.LT"
    script3 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/b-long-september.txt runs/experiments/seupd2223-JIHUMING-09_fr_fr_4gram/seupd2223-JIHUMING-09_fr_fr_4gram.LT"
    script4 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/b-long-september.txt runs/experiments/seupd2223-JIHUMING-10_fr_fr_5gram/seupd2223-JIHUMING-10_fr_fr_5gram.LT"
    script5 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/b-long-september.txt runs/experiments/seupd2223-JIHUMING-12_fr_fr_4gram_ner/seupd2223-JIHUMING-12_fr_fr_4gram_ner.LT"
    total_script = 5
    outFileName = "runs/experiments/scores/LT_scores"
    scoretype = "LT"
if run == 4:
    # trec_eval all_trec commands - LONG TERM
    script1 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/heldout-test.txt runs/experiments/seupd2223-JIHUMING-07_fr_fr/seupd2223-JIHUMING-07_fr_fr.WT"
    script2 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/heldout-test.txt runs/experiments/seupd2223-JIHUMING-08_fr_fr_3gram/seupd2223-JIHUMING-08_fr_fr_3gram.WT"
    script3 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/heldout-test.txt runs/experiments/seupd2223-JIHUMING-09_fr_fr_4gram/seupd2223-JIHUMING-09_fr_fr_4gram.WT"
    script4 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/heldout-test.txt runs/experiments/seupd2223-JIHUMING-10_fr_fr_5gram/seupd2223-JIHUMING-10_fr_fr_5gram.WT"
    script5 = "../trec_eval-9.0.7/trec_eval -m all_trec runs/experiments/longeval-relevance-judgements/heldout-test.txt runs/experiments/seupd2223-JIHUMING-12_fr_fr_4gram_ner/seupd2223-JIHUMING-12_fr_fr_4gram_ner.WT"
    total_script = 5
    outFileName = "runs/experiments/scores/WT_scores"
    scoretype = "WT"

# Clear maps score file
with open(outFileName, 'w') as f:
    pass

for i in range(1, total_script+1):
    script = eval('script' + str(i))
    result = subprocess.run(script, shell=True, capture_output=True, text=True)
    with open('runs/experiments/scores/output' + scoretype + str(i) + '.txt', 'w') as f:
        f.write(result.stdout)
    # Get the lines of the output
    title_line = result.stdout.split('\n')[0]
    map_line = result.stdout.split('\n')[5]
    rprec_line = result.stdout.split('\n')[7]
    ndcg_line = result.stdout.split('\n')[55]

    # Save the line to a text file
    with open(outFileName + '.txt', 'a') as f:
        f.write(f"{title_line}\n{map_line}\n{rprec_line}\n{ndcg_line}\n")