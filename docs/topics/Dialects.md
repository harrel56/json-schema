# Dialects

Officially supported dialects (JSON Schema specification versions):
- Draft 2020-12,
- Draft 2019-09,
- Draft 07,
- Draft 06,
- Draft 04.

It is automatically inferred (by the value of the `$schema` keyword) which dialect to use.
If a schema does not contain the `$schema` keyword, the default dialect will be used, which is [draft 2020-12 dialect](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Dialects.Draft2020Dialect.html).
It's possible to change the default dialect by calling:

```java
new ValidatorFactory().withDefaultDialect(new Dialects.Draft2019Dialect());
```

## Meta-schemas
Each schema is recommended to contain the `$schema` keyword to properly infer which dialect to use.
`$schema` keyword must refer to a meta-schema against which the current schema will be validated.
Resolution of meta-schemas follows the same [rules](Resolving-schemas.md#using-multiple-resolvers) as for regular schemas.

> If resolution of meta-schema is unsuccessful, [MetaSchemaResolvingException](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/MetaSchemaResolvingException.html) is thrown.

> If validation against meta-schema fails, [InvalidSchemaException](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/InvalidSchemaException.html) is thrown.

There is a configuration option that disables all schema validations:

```java
new ValidatorFactory().withDisabledSchemaValidation(true);
```

Note that this option will also turn off meta-schema resolution mechanics
and will fall back to the matching predefined dialect or the default one.

## Importance of dialects

Every schema needs to be understood in the context of a specific dialect.
Dialects consist of a few vital parts:
1. [**Specification version**](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/SpecificationVersion.html) - 
parsing schemas might behave differently depending on version value.
2. [**Evaluator Factory**](https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/EvaluatorFactory.html) - 
class responsible for the creation of evaluators which in turn contain (mostly) validation logic.
3. **Meta-schema URI** (optional) - every schema in this dialect must be valid against this meta-schema.
4. **Vocabularies** (optional) - first appeared in the Draft 2019-09 specification, but it's not strictly limited to the newest versions only.
You can define which vocabularies are supported, required and enabled by default.

<table style='both'>
    <tr>
        <td></td>
        <td>Draft 2020-12</td>
        <td>Draft 2019-09</td>
        <td>Draft 07</td>
        <td>Draft 06</td>
        <td>Draft 04</td>
    </tr>
    <tr>
        <td>Spec version</td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/SpecificationVersion.html#DRAFT2020_12'>
            SpecificationVersion.DRAFT2020_12
        </a></td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/SpecificationVersion.html#DRAFT2019_09'>
            SpecificationVersion.DRAFT2019_09
        </a></td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/SpecificationVersion.html#DRAFT7'>
            SpecificationVersion.DRAFT7
        </a></td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/SpecificationVersion.html#DRAFT6'>
            SpecificationVersion.DRAFT6
        </a></td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/SpecificationVersion.html#DRAFT4'>
            SpecificationVersion.DRAFT4
        </a></td>
    </tr>
    <tr>
        <td>Evaluator factory</td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Draft2020EvaluatorFactory.html'>
            Draft2020EvaluatorFactory
        </a></td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Draft2019EvaluatorFactory.html'>
            Draft2019EvaluatorFactory
        </a></td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Draft7EvaluatorFactory.html'>
            Draft7EvaluatorFactory
        </a></td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Draft6EvaluatorFactory.html'>
            Draft6EvaluatorFactory
        </a></td>
        <td><a href='https://javadoc.io/doc/dev.harrel/json-schema/latest/dev/harrel/jsonschema/Draft4EvaluatorFactory.html'>
            Draft4EvaluatorFactory
        </a></td>
    </tr>
    <tr>
        <td>Meta-schema</td>
        <td>https://json-schema.org/draft/2020-12/schema</td>
        <td>https://json-schema.org/draft/2019-09/schema</td>
        <td>http://json-schema.org/draft-07/schema#</td>
        <td>http://json-schema.org/draft-06/schema#</td>
        <td>http://json-schema.org/draft-04/schema#</td>
    </tr>
    <tr>
        <td>Supported vocabs</td>
        <td><ul>
            <li>https://json-schema.org/draft/2020-12/vocab/core</li>
            <li>https://json-schema.org/draft/2020-12/vocab/applicator</li>
            <li>https://json-schema.org/draft/2020-12/vocab/unevaluated</li>
            <li>https://json-schema.org/draft/2020-12/vocab/validation</li>
            <li>https://json-schema.org/draft/2020-12/vocab/meta-data</li>
            <li>https://json-schema.org/draft/2020-12/vocab/format-annotation</li>
            <li>https://json-schema.org/draft/2020-12/vocab/format-assertion</li>
            <li>https://json-schema.org/draft/2020-12/vocab/content</li>
        </ul></td>
        <td><ul>
            <li>https://json-schema.org/draft/2019-09/vocab/core</li>
            <li>https://json-schema.org/draft/2019-09/vocab/applicator</li>
            <li>https://json-schema.org/draft/2019-09/vocab/validation</li>
            <li>https://json-schema.org/draft/2019-09/vocab/meta-data</li>
            <li>https://json-schema.org/draft/2019-09/vocab/format</li>
            <li>https://json-schema.org/draft/2019-09/vocab/content</li>
        </ul></td>
        <td>-</td>
        <td>-</td>
        <td>-</td>
    </tr>
    <tr>
        <td>Required vocabs</td>
        <td><ul>
            <li>https://json-schema.org/draft/2020-12/vocab/core</li>
        </ul></td>
        <td><ul>
            <li>https://json-schema.org/draft/2019-09/vocab/core</li>
        </ul></td>
        <td>-</td>
        <td>-</td>
        <td>-</td>
    </tr>
</table>


## Custom dialects

Please see a dedicated chapter [Custom dialects](Custom-dialects-details.md).