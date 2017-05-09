# ArgParser Binder: Simple and comfy

[![Build Status](https://travis-ci.org/mibac138/ArgParser.svg?branch=master)](https://travis-ci.org/mibac138/ArgParser) 

[Javadoc](https://mibac138.github.com/ArgParser/javadoc/binder)

Binder is a layer on top of ArgParser to make working with it easier and more comfy

## Getting started

The first step is to add Binder to your project:

```groovy
// Core (required)
compile 'com.github.mibac138.argparser:argparser-core:1.0.0'
// Binder
compile 'com.github.mibac138.argparser:argparser-binder:1.0.0'
```

2. Bind your method(s). You have several options at this point:
- bind a specific method (`Binder.bind(object, object.class.getMethod("name", /* params */))`),
- bind a specific method using Kotlin (`Binder.bind(object::method)`),
- or bind the whole class (`Binder.bind(object)`, every method meant to be bound must be annotated with `@BindMethod`)

3. Invoke your binding
- from Java
```java
binding.invoke(asReader("args"), /* parser */ new SimpleParserRegistry())
 ```
 - from Kotlin
 ```kotlin
 binding.invoke("args".asReader(), /* parser */ SimpleParserRegistry())
 ```

## Communication

- [GitHub Issues](https://github.com/mibac138/ArgParser/issues)
- [Discord](https://discord.gg/9wxjQuv)

## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/mibac138/ArgParser/issues).

 
## LICENSE

Copyright (c) 2017 Michał Bączkowski

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.