# Annotations

Annotation collection is a process that happens alongside validation. Annotations are only present in a successful validation result:
```java
Validator.Result result = new ValidatorFactory().validate(schema, instance);

// Collected annotation during the validation process
List<Annotation> annotations = result.getAnnotations(); 
```

Annotation collection is described in detail in [specification](https://json-schema.org/draft/2020-12/json-schema-core#name-annotations).
Some annotation will be discarded and not present in the final result.

> The default behavior of provided evaluator factories is to treat any unknown keyword as an annotation (as long as its value is of a string type).
{style="note"}

## Annotation type

[Documentation](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Annotation.html).

<deflist>
<def title='annotation (Object)'>
Annotation value. Can be of any type.

Example: `List.of("foo")`
</def>
<def title='evaluationPath (String)'>
JSON pointer representing evaluation point in schema JSON.

Example: `/properties`
</def>
<def title='instanceLocation (String)'>
JSON pointer representing evaluation point in instance JSON.

Example: `/data`
</def>
<def title='keyword (String)'>
Keyword name associated with given evaluation point.

Example: `properties`
</def>
<def title='schemaLocation (String)'>
Absolute schema location (URI) which uniquely identifies given schema.

Example: `https://harrel.dev/3077cb97#`
</def>
</deflist>

## Provided annotations

Some keywords defined by specification also define the annotation which they should produce:

<table>
    <tr>
        <td>Keyword</td>
        <td>Description</td>
        <td>Type</td>
        <td>Example</td>
    </tr>
    <tr>
        <td><code>properties</code></td>
        <td rowspan='4'>Set of property names which were validated by this keyword.</td>
        <td rowspan='4'><code>Set&lt;String&gt;</code></td>
        <td rowspan='4'><code>Set.of("prop1", "prop2")</code></td>
    </tr>
    <tr>
        <td><code>additionalProperties</code></td>
    </tr>
    <tr>
        <td><code>patternProperties</code></td>
    </tr>
    <tr>
        <td><code>unevaluatedProperties</code></td>
    </tr>
    <tr>
        <td><code>prefixItems</code></td>
        <td>Number of items validated by this keyword. If all items were validated, then <code>Boolean.TRUE</code>.</td>
        <td><code>Integer</code> or <code>Boolean.TRUE</code></td>
        <td><code>2</code>, <code>Boolean.TRUE</code></td>
    </tr>
    <tr>
        <td><code>items</code> (legacy)</td>
        <td>Number of items validated by this keyword. If all items were validated, then <code>Boolean.TRUE</code>.</td>
        <td><code>Integer</code> or <code>Boolean.TRUE</code></td>
        <td><code>2</code>, <code>Boolean.TRUE</code></td>
    </tr>
    <tr>
        <td><code>items</code> (Draft 2020-12)</td>
        <td rowspan='3'><code>Boolean.TRUE</code> if validated successfully, meaning all remaining items were validated.</td>
        <td rowspan='3'><code>Boolean.TRUE</code></td>
        <td rowspan='3'><code>Boolean.TRUE</code></td>
    </tr>
    <tr>
        <td><code>additionalItems</code></td>
    </tr>
    <tr>
        <td><code>unevaluatedItems</code></td>
    </tr>
    <tr>
        <td><code>contains</code></td>
        <td>List of item indices (ordered) which validated against <code>contains</code> schema.</td>
        <td><code>List&lt;Integer&gt;</code></td>
        <td><code>List.of(0, 4, 5)</code></td>
    </tr>
</table>

## Examples

### Object

<table>
    <tr>
        <td>Schema</td>
        <td>Instance</td>
    </tr>
    <tr>
<td>

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "urn:object",
  "properties": {
    "foo": {
      "title": "foo schema",
      "type": "number"
    },
    "bar": {
      "$ref": "#/$defs/bar"
    },
    "baz": false
  },
  "unevaluatedProperties": true,
  "$defs": {
    "bar": {
      "const": "bar"
    }
  }
}
```

</td>
<td>

```json
{
  "foo": 1,
  "bar": "bar",
  "bax": {}
}
```

</td>
    </tr>
</table>

Produces the following annotations:

<table>
    <tr>
        <td><code>annotation</code></td>
        <td><code>evaluationPath</code></td>
        <td><code>instanceLocation</code></td>
        <td><code>keyword</code></td>
        <td><code>schemaLocation</code></td>
    </tr>
    <tr>
        <td><code>"foo schema"</code></td>
        <td>/properties/foo/title</td>
        <td>/foo</td>
        <td>title</td>
        <td>urn:object#/properties/foo</td>
    </tr>
    <tr>
        <td><code>Set.of("bar", "foo")</code></td>
        <td>/properties</td>
        <td>(empty string)</td>
        <td>properties</td>
        <td>urn:object#</td>
    </tr>
    <tr>
        <td><code>Set.of("bax")</code></td>
        <td>/unevaluatedProperties</td>
        <td>(empty string)</td>
        <td>unevaluatedProperties</td>
        <td>urn:object#</td>
    </tr>
</table>

### Array

<table>
    <tr>
        <td>Schema</td>
        <td>Instance</td>
    </tr>
    <tr>
<td>

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "urn:array",
  "prefixItems": [
    { "type": "boolean" },
    { "type": "string" }
  ],
  "items": {
    "type": "number"
  },
  "contains": {
    "unknownKeyword": "value",
    "type": "number",
    "multipleOf": 2
  }
}
```

</td>
<td>

```json
[
  true,
  "second value",
  1,
  2,
  3,
  4
]
```

</td>
    </tr>
</table>

Produces the following annotations:

<table>
    <tr>
        <td><code>annotation</code></td>
        <td><code>evaluationPath</code></td>
        <td><code>instanceLocation</code></td>
        <td><code>keyword</code></td>
        <td><code>schemaLocation</code></td>
    </tr>
    <tr>
        <td><code>"value"</code></td>
        <td>/contains/unknownKeyword</td>
        <td>/3</td>
        <td>unknownKeyword</td>
        <td>urn:array#/contains</td>
    </tr>
    <tr>
        <td><code>"value"</code></td>
        <td>/contains/unknownKeyword</td>
        <td>/5</td>
        <td>unknownKeyword</td>
        <td>urn:array#/contains</td>
    </tr>
    <tr>
        <td><code>List.of(3, 5)</code></td>
        <td>/contains</td>
        <td>(empty string)</td>
        <td>contains</td>
        <td>urn:array#</td>
    </tr>
    <tr>
        <td><code>2</code></td>
        <td>/prefixItems</td>
        <td>(empty string)</td>
        <td>prefixItems</td>
        <td>urn:array#</td>
    </tr>
    <tr>
        <td><code>true</code></td>
        <td>/items</td>
        <td>(empty string)</td>
        <td>items</td>
        <td>urn:array#</td>
    </tr>
</table>