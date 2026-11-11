# Bayesian Network Classifier (Java)

A Bayesian network classifier written from scratch in Java. It learns the network structure from data with a **greedy hill climber** scored by **Minimum Description Length (MDL)**, estimates the probability tables with Laplace smoothing, and classifies new examples. Two Swing desktop apps let you train a model and then use it to classify.

Built as the group project for *Algorithms and Computational Modelling* (Instituto Superior Técnico, 2025/2026).

## Highlights

- **Structure learning:** hill climbing with random restarts over directed acyclic graphs, with at most *k* parents per attribute (plus the class).
- **Incremental scoring:** each move (adding or removing an edge; reversal is also supported) is scored by its MDL *delta*, so the whole network is never rescored.
- **Fast counting:** the information-gain term is computed in a single pass over the data (O(N) instead of O(N × domain size)).
- **Cycle checks** with breadth-first search before every edge is accepted.
- **Evaluation** with leave-one-out cross-validation.
- **Two GUIs:** `LearningApp` trains and saves a network; `ClassifyingApp` loads it and predicts the class of a new patient.

## Results

Leave-one-out accuracy with at most 2 parents and smoothing S = 0.5:

| Dataset | Examples | Accuracy |
|---|---:|---:|
| Thyroid | 2,642 | 96.90% |
| Soybean (large) | 562 | 92.35% |
| Hepatitis | 80 | 88.75% |
| Satimage | 6,435 | 87.13% |
| Breast cancer | 683 | 82.72% |
| Diabetes | 768 | 78.39% |

## How it works

Every attribute has the class as a parent, and the hill climber adds up to *k* extra parents between attributes. The search starts from the empty graph and from several random graphs, keeps adding or removing any edge that lowers the MDL score, and stops at a local optimum. The best graph across all restarts becomes the final model.

```
MDL(G) = (|θ| / 2) · log₂(N) − LL(G)
```

where `|θ|` is the number of free parameters and `LL` is the log-likelihood of the data.

## Project structure

```
src/projecto/
├── Amostra.java         # dataset: loads the CSV, counts value combinations
├── Graphoo.java         # directed graph, cycle checks, MDL and MDL delta
├── BN.java              # Bayesian network: parameter learning and classification
├── Validation.java      # hill climbing and leave-one-out validation
├── LearningApp.java     # GUI to train and save a network
└── ClassifyingApp.java  # GUI to load a network and classify
data/                    # discretised datasets (last column is the class)
```

(`Amostra` is Portuguese for *sample*.)

## Running it

Requires Java 11 or later.

```bash
javac -d out src/projecto/*.java
cd data

# Train a network and save it (pick a CSV, max parents and number of random restarts)
java -cp ../out projecto.LearningApp

# Load the saved network and classify a new example
java -cp ../out projecto.ClassifyingApp

# Reproduce the leave-one-out results (edit main() to choose the dataset)
java -cp ../out projecto.Validation
```

## Data

All attributes are discretised to integers starting at 0, and the last column is the class. The datasets were provided by the course and come from classic machine learning benchmarks (UCI Machine Learning Repository).

## Authors

Carolina Baptista and group colleagues, Instituto Superior Técnico.
