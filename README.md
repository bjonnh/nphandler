# nphandler

This is super experimental. Don't ask questions.

## How to use

You need a JVM installed.

```shell
./gradlew run --args="-i public_data/demo.ods -o output/output.dot"
```

## Conversion

You can then export the generated .dot using one of the folowing:

```
dot -Tsvg output/output.dot -o output/output.svg
dot -Teps output/output.dot -o output/output.eps
dot -Tpdf output/output.dot -o output/output.pdf
dot -Tpng output/output.dot -o output/output.png
```
