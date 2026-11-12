"""Draw the README figures (pip install matplotlib; python figures/make_figures.py).

The network is the structure LearningApp found on data/diabetes.csv with at most
2 parents, 10 random restarts and S = 0.5 (edges copied from Graphoo.toString()).
The accuracies are the leave-one-out results listed in Validation.main().
"""
import os

import matplotlib.pyplot as plt
from matplotlib.patches import FancyArrowPatch

HERE = os.path.dirname(os.path.abspath(__file__))
INK, MUTED, GRID, LINE = "#0b0b0b", "#52514e", "#e4e3df", "#b9b8b2"
BLUE, ORANGE = "#2a78d6", "#eb6834"
plt.rcParams.update({"font.family": "DejaVu Sans", "font.size": 11, "text.color": INK,
                     "axes.labelcolor": MUTED, "xtick.color": MUTED, "ytick.color": MUTED})

# 0-based attribute indices, as printed by Graphoo (parent, child)
EDGES = [(0, 7), (3, 4), (3, 5), (4, 1), (7, 2), (7, 3)]
N_ATTR = 8


def network():
    # Attributes on a circle, ordered so learned edges join near neighbours;
    # the class sits in the middle because it is a parent of every attribute
    from math import cos, pi, sin
    order = [0, 7, 2, 3, 5, 4, 1, 6]
    pos = {node: (2.2 * cos(pi / 2 - 2 * pi * i / N_ATTR), 2.2 * sin(pi / 2 - 2 * pi * i / N_ATTR))
           for i, node in enumerate(order)}
    cls = (0.0, 0.0)

    fig, ax = plt.subplots(figsize=(8, 7), facecolor="white")

    def arrow(a, b, color, lw, ls="-", rad=0.0):
        ax.add_patch(FancyArrowPatch(a, b, arrowstyle="-|>", mutation_scale=16, color=color,
                                     lw=lw, ls=ls, shrinkA=22, shrinkB=22, zorder=1,
                                     connectionstyle=f"arc3,rad={rad}"))

    for node, xy in pos.items():
        arrow(cls, xy, LINE, 1.2, "--")
    for p, c in EDGES:
        arrow(pos[p], pos[c], BLUE, 2.4)
    for node, (x, y) in pos.items():
        ax.scatter(x, y, s=1300, color="white", edgecolor=BLUE, linewidth=2.5, zorder=2)
        ax.text(x, y, f"A{node + 1}", ha="center", va="center", fontsize=12, zorder=3)
    ax.scatter(*cls, s=1500, color=ORANGE, edgecolor="white", linewidth=2.5, zorder=2)
    ax.text(*cls, "Class", ha="center", va="center", fontsize=11, color="white",
            fontweight="bold", zorder=3)

    ax.plot([], [], color=BLUE, lw=2.4, label="edge learned by hill climbing (MDL)")
    ax.plot([], [], color=LINE, lw=1.2, ls="--", label="class → every attribute (fixed)")
    ax.legend(frameon=False, loc="lower center", bbox_to_anchor=(0.5, -0.06), ncol=1)
    ax.set_title("Network learned on the Diabetes dataset\n(8 attributes, at most 2 parents each)",
                 fontsize=13)
    ax.set_aspect("equal")
    ax.set_xlim(-2.9, 2.9)
    ax.set_ylim(-3.0, 2.8)
    ax.axis("off")
    fig.tight_layout()
    fig.savefig(os.path.join(HERE, "learned_network.png"), dpi=150, facecolor="white")
    plt.close(fig)


def accuracy():
    results = [("Thyroid", 96.90), ("Soybean (large)", 92.35), ("Hepatitis", 88.75),
               ("Satimage", 87.13), ("Breast cancer", 82.72), ("Diabetes", 78.39)]
    names = [r[0] for r in results][::-1]
    values = [r[1] for r in results][::-1]
    fig, ax = plt.subplots(figsize=(9, 3.8), facecolor="white")
    ax.barh(names, values, color=BLUE, height=0.6)
    for y, v in enumerate(values):
        ax.text(v - 1, y, f"{v:.2f}%", ha="right", va="center", color="white", fontsize=11)
    ax.set_xlim(0, 100)
    ax.set_xlabel("Leave-one-out accuracy (%)")
    ax.grid(axis="x", color=GRID, lw=1)
    ax.set_axisbelow(True)
    for side in ("top", "right", "left"):
        ax.spines[side].set_visible(False)
    ax.spines["bottom"].set_color(GRID)
    ax.tick_params(length=0)
    ax.tick_params(axis="y", colors=INK)
    ax.set_title("Accuracy on six benchmark datasets", loc="left", fontsize=13)
    fig.tight_layout()
    fig.savefig(os.path.join(HERE, "accuracy.png"), dpi=150, facecolor="white")
    plt.close(fig)


if __name__ == "__main__":
    network()
    accuracy()
