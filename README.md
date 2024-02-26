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
  * [ X ] Build & push the image
  * [  ] Add maven linting/code coverage
  * [  ] Add helm linting
  * [  ] Build & push the chart
  * [  ] Add image scanning
  * [ ] Use the chart in cluster
  * [ ] Trigger the pipeline on push
* [ ] Monitoring
  * [ ] Add Micrometer
  * [ ] Add OpenTelemetry
*  [ ] Pipeline improvements
  * [  ] block image build if maven linting fails
  * [  ] block chart build if helm linting fails
  * [  ] block chart build if image scanning fails
  * [  ] only push tag if both image and maven linting pass