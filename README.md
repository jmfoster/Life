# Life

[See project overview in Psychonomics 2011 conference poster](https://github.com/jmfoster/Life/blob/master/Project_Overview.pdf)

## Project Goal
This computational model explores analogical structure mapping and schema induction in the domain of Conway’s Game of Life. Life runs as a cellular automaton with hierarchical emergent structure. The goal of the model is to discover recurring patterns and object hierarchies.

## Model

### Input
The input to the model is a relational graph representing basic interactions between clusters of cells. The clusters act as basic level objects and are based on contiguous “on” cells and span multiple iterations of the Life simulation. The only relation in the input graph is a basic level relation, which is created when there is a merge or split of basic level objects. All the basic level objects and relations are part of the worldgraph which is the input to the computational model. Each basic level object and relation has a feature vector with dimensions for its type, role fillers, and relations it participates in.

### Operation

The model creates an attentional spotlight on a section of the worldgraph and probes memory with the nodes’ feature vectors (i.e., MAC Vectors) to retrieve a set of feature matches. These feature matches are passed to the analogical mapping stage (i.e., FAC stage) which is seeded with mapping weights between nodes in the probe and each of the feature matches. The initial mapping weights spread to neighboring nodes based on mapping dynamics simplified from CAB (Larkey & Love, 2003) and formalized in [Foster & Jones (2017)](http://arxiv.org/abs/1712.10070).

There is an arbitrary working memory constraint on the size of the analogical match because the source and target domains are not pre-circumscribed. The source and target domains are both part of the larger worldgraph so there must be some means of limiting the scope of the analogy. The mapping dynamics are iterated until convergence and the analogical quality is evaluated based on systematicity and parallelism.

The analogical mapping indicates a shared pattern of role binding in the source and target graphs. This pattern of role binding is encoded with a schema inducted by recruiting a new node to represent each mapped node pair in the analogy. The schema nodes are connected based on mutual connectivity in the source and target graph (see Doumas et al., 2008; Hummel & Holyoak, 2003). In other words, if two nodes are connected in both the source and target, the corresponding nodes are connected in the schema. This method of schema induction is a form of intersection discovery.

The schema is added back into the worldgraph and is available for retrieval in future memory probings, just as episodes are. In this way, schemas can be retrieved through the MAC/FAC process and refined by producing a yet more abstract schema that encodes the intersecting relational structure between the probe and the retrieved schema.

### Results

Simulations with the model have revealed the issue of schema evaporation, where retrieved schemas are refined by intersection discovery until their size reaches zero. Together with experimental evidence from Corral & Jones (2012), the schema evaporation issue suggests a need for schema elaboration in addition to schema refinement.

Combining analogy and schema induction in this rich compositional domain results in discovery of many frequent but mostly useless schemas (see Figure for an example). Critically, this unsupervised model can only be guided by the analogies that are discovered, the quality of those analogies, and some notion of intrinsic coherence of the schema. Preliminary simulations have revealed a need for an additional mechanism to evaluate schemas. My [Analogical Reinforcement Learning project](https://github.com/jmfoster/arl) implements simulations with a model that includes such a guiding mechanism that learns to play tic-tac-toe (also see [Foster & Jones (2017)](http://arxiv.org/abs/1712.10070).
