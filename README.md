# Checked Exception Streams

**Checked Exception Streams** (ChkStreams) adds checked exception support to the
Java 8 Stream API.

ChkStreams is not an official Google product.


## Overview

Have you ever wanted to write something like this, but been foiled by the
dreaded `Unhandled exception type IOException`?

```java
filenames.parallelStream()
  .map(Files::readFully)
  .collect(toList());
```

Be foiled no more!

```java
ChkStreams.of(filenames.parallelStream())
  .canThrow(IOException.class)
  .map(Files::readFully)
  .collect(toList());
```


## Usage

ChkStreams extends the existing Java 8 Stream API. To use it, simply:
1.  Wrap any `Stream` using `ChkStreams.of(Stream)` to get a `ChkStream`.
2.  Declare one or more checked exceptions by calling
    `ChkStream#canThrow(Exception)`, so that subsequent stream operations will
    allow that `Exception`.
3. Use the same Stream API you're used to (except now your lambdas can throw the
   declared exceptions!)
4. Handle the checked exceptions in the usual way (catch or declare thrown) in
   any method that invokes a **terminal** operation on the stream.


## Features

* Adds checked exception support to the familiar Streams API!
* Exceptions are enforced by the compiler in the usual way, and need only be
  handled when invoking terminal operations.
* Mutually compatible with regular Java `Stream`s (See `ChkStreams#of(Stream)`
  and `ChkStream#toStream()`)
* (Optional) support for the
  [StreamSupport backport](https://streamsupport.sourceforge.io/) and
  [Retrolambda](https://github.com/orfjackal/retrolambda). Enjoy the power of
  `ChkStream` on Java 6+ and Android!
* May cause you to barf rainbows.


## Limitations

* A maximum of 5 checked exceptions may be added to a `ChkStream`.
* No support for the primitive stream specializations like `IntStream` (yet).
  Use boxed streams for those for now. (This is not a fundamental limitation,
  just a time investment issue. File a bug if you want this!).
* No interfaces in common with `Stream` or between different `ChkStream`s.


## License

This project is licensed under the GNU GPLv2 with Classpath Exception, which is
the same license as OpenJDK itself.
