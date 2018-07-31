# DM Templating Library

This library provides a soy-based (go/soy) templating language designed for
crafting Deployment Manager packages.

DM packages contain Jinja template files. So this templating language is used
as a super-templating language, used for creating templates that produce Jinja
template. DM's Jinja templating system in turn is used for creating YAML files.

As a result, this templating language extends soy to make it easy (1) to
preserve YAML's line break and space sensitivity, and (2) to embed Jinja
constructs.

## Preprocessing

To support (1) and (2) above, the soy templates are preprocessed by
[`Preprocessor.java`](http://cs/piper///depot/google3/third_party/java_src/deployment_manager_autogen/java/com/google/cloud/deploymentmanager/autogen/soy/Preprocessor.java).

### Preserving Indentations and Line Breaks

By defaults, soy joins all lines, dropping spaces. There isn't a hook in soy to
alter this behavior. We work around by inserting special characters to force
line breaks and preserve indetations.

For example, an input like:

    items:
      - name: {$key}
        value: {$value}

will be preprocessed to become:

    {nil}items{\n}
    {nil}  - name: {$key}{\n}
    {nil}    value: {$value}{\n}

The preprocessor does more than just simply inserting such special characters
into every line. This is because some lines should not need them. For example,
for this snippet:

    parentX:
    {if $someVariable}
      propertyA: {$someVariable}
    {/if}
      propertyB: {$propertyB}

only the `{if}` and `{/if}` lines should not have forced line breaks, or it will
result in 2 empty lines. The correct preprocessed content should look like:

    {nil}parentX:{\n}
    {if $someVariable}
    {nil}  propertyA: {$someVariable}{\n}
    {/if}
    {nil}  propertyB: {$propertyB}{\n}

The author of the template can suppress the default line breaking behavior on
a single line by explicitly inserting at least one special spacing character
on that line.

See [tips](#tips) for more on how to make the preprocessor work in your favor.

### Supporting Jinja Delimiters

Jinja delimiters such as `{{ ... }}` or `{% ... %}` can be used directly in the
templates. The preprocessor will recognize them and automatically escape them.
For example:

    {{ some_variable }}

will be rewritten as:

    {lb}{lb} some_variable {rb}{rb}

## Tips

### Examples

The following file demonstrates several common use cases: this
[soy file](http://cs/piper///depot/google3/javatests/com/google/cloud/deploymentmanager/autogen/soy/testdata/sanity_check.jinja.soy)
produces this
[jinja file](http://cs/piper///depot/google3/javatests/com/google/cloud/deploymentmanager/autogen/soy/testdata/sanity_check.jinja).

### Empty Lines

By default all empty lines are collapsed. To force an empty line write a
single `{\n}` on that line. This is useful when you want to insert an empty
line to separate sections in the resulting output.

    inputs:
      - propertyA

    {\n}
    outputs:
      - outputA

### Single Line `{if}`

Consider the following:

    {if $variableA}{$variableA}{/if}

The processor will rewrite this as:

    {nil}{if $variableA}{$variableA}{/if}{\n}

So when the if command evaluates to false, an empty line will result. In this
case you should insert your own line break to correct this behavior:

    {if $variableA}{$variableA}{\n}{/if}

Note that the following works correctly without manual line breaking and is
recommended:

    {if $vairableA}
    {$variableA}
    {/if}

### Text `{let}`

Consider the following:

    {let $var kind="text"}{$dad} is a parent of {$child}{/let}

This is useful in a lot of cases, instead of clumsier expressions like:

    {let $var: $dad + ' is a parent of ' + $child}

It can also let you store the output of a subtemplate:

    {let $var}{call .subtemplate/}{/let}

There is one problem with this, however. The preprocessor does rewrite this
into:

    {nil}{let $var kind="text"}{$dad} is a parent of {$child}{/let}{\n}

resulting into an empty line. This is because the preprocessor sees print
comands and text on this line. You should insert a `{nil}` to prevent the
preprocessor from forcing linebreaks here, like so:

    {let $var kind="text"}{$dad} is a parent of {$child}{/let}{nil}

Note that in the subtemplate calling case, you do not need to manually
insert `{nil}`.

### Sub-template Trailing Line Breaks

Consider the following sub-template:

    {template .sub}
    my name is
    {/template}

If you call this template:

    {call .sub} John

there will be a line break right before " John". To prevent this, insert a
`{nil}` on the last line of your sub-template, like so:

    {template .sub}
    my name is{nil}
    {/template}

### Indenting a Sub-template Rendering

To indent the output of a sub-template, first store it in a variable and then
use the `|indent` print directive.

    {let $resource kind="text"}{call .createResource/}{/let}
    resources:
      - {$resource |indent: 4}
