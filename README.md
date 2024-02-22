# ingress-dns-export-controller

A simple operator that copies the value in a spec to a ConfigMap.

## TODO

* [ ] Add tests
* [ X ] buid a docker image
  * [ X ] Using Azul
  * [ X ] Using a multi-stage build
  * [ X ] Multi-arch build
  * [ ] Using Wolfi
* [ X ] Add a helm chart
  * [ X ] Build & push the chart
  * [  ] Use the chart in cluster
* [ ] Create Tekton pipeline
  * [ ] Build & push the image
  * [ ] Build & push the chart
  * [ ] Trigger the pipeline on push

