# IEval

Evaluation software for Information Extraction tasks. Currently includes BioNLP-ST 2016 and BioNLP-OST 2019 SeeDev and Bacteria Biotopes tasks.
Evaluation of TAEC and EPOP coming soon.

* [BioNLP-OST 2019 website](https://2019.bionlp-ost.org/)
  * [Bacteria Biotopes 2019 task](https://sites.google.com/view/bb-2019)
  * [SeeDev task](https://sites.google.com/view/seedev2019)
* [BioNLP-ST 2016 website](http://2016.bionlp-st.org/)

The current version contains the data for evaluating on training and development sets for SeeDev and BB 2019 tasks. For the evaluation on the test set of the BB 2016, please use the [online service](http://bibliome.jouy.inra.fr/demo/BioNLP-ST-2016-Evaluation/index.html).

# How to use

## Prerequisites

* Java >= 8
* Apache Maven >= 3.3

## Build

```
git clone https://github.com/Bibliome/bionlp-st.git
cd bionlp-st
mvn clean install
```

Maven produces a JAR file in `bionlp-st-core/target/bionlp-st-core-0.1.jar`.

## Usage

```
java -jar bionlp-st-core-0.1.jar -help
java -jar bionlp-st-core-0.1.jar -list-tasks
java -jar -task TASK -train|-dev -prediction PRED [-alternate] [-detailed] [-force]
```

where
* `TASK` is the short name of the task. Available tasks are:
  * `SeeDev-full`
  * `SeeDev-binary`
  * `BB19-norm`
  * `BB19-rel`
  * `BB19-kb`
  * `BB19-norm+ner`
  * `BB19-rel+ner`
  * `BB19-kb+ner`
  * `BB-cat`
  * `BB-event`
  * `BB-kb`
  * `BB-cat+ner`
  * `BB-event+ner`
  * `BB-kb+ner`
* `PRED` is the location of your predictions (`.a2` files), either a directory of a ZIP archive.
* Specify `-alternate` to display several additional measures.
* Specify `-detailed` to display a document-per-document evaluation, including reference-prediction pairings.
* Specify `-force` to evaluate even if errors where found.
