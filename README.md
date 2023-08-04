# Vitruv Tool Adapters
[![Latest Release](https://img.shields.io/github/release/vitruv-tools/Vitruv-Tool-Adapters.svg)](https://github.com/vitruv-tools/Vitruv-Tool-Adapters/releases/latest)
[![Issues](https://img.shields.io/github/issues/vitruv-tools/Vitruv-Tool-Adapters.svg)](https://github.com/vitruv-tools/Vitruv-Tool-Adapters/issues)
[![License](https://img.shields.io/github/license/vitruv-tools/Vitruv-Tool-Adapters.svg)](https://raw.githubusercontent.com/vitruv-tools/Vitruv-Tool-Adapters/main/LICENSE)

**!Development of this project is discontinued!**

[Vitruvius](https://vitruv.tools) is a framework for view-based software development.
It assumes different models to be used for describing a software system, which are automatically kept consistent by the framework executing (semi-)automated rules that preserve consistency.
These models are modified only via views, which are projections from the underlying models.
For general information on Vitruvius, see our [GitHub Organisation](https://github.com/vitruv-tools) and our [Wiki](https://github.com/vitruv-tools/.github/wiki).

This project contains a set of adapters for views, such that a V-SUM (Virtual Single Underlying Model) can be accessed from certain tools.
With the provided adapters, models can be modified in these tools and changes are propagated to and kept consistent in an underlying V-SUM.
Currently, adapters for the Eclipse IDE and in particular for modifying EMF models in any kind of editor, as well as a wizard to setup and use a V-SUM in Eclipse are provided.

## Installation

Vitruvius can be installed in Eclipse via the [nightly update site](https://vitruv.tools/updatesite/nightly). A wiki page provides [detailed instructions for using or extending Vitruvius](https://github.com/vitruv-tools/.github/wiki/Getting-Started).
