# big-sequence-file

a big file, similar to java embedded implementation of ``kafka``, providing sequential data access.

## QuickStart

- Sample Code

``` java
BigSequenceFile bsf = null;

try {
  bsf = new BigSequenceFile("hello.bsf");
  bsf.open();

  bsf.appendTrunk("abc".getBytes());
  bsf.appendTrunk("def".getBytes());
  bsf.appendTrunk("g".getBytes());

  byte[] trunk = bsf.deductTrunk();
  System.out.println(new String(trunk));

} finally {
  if (bsf != null) {
    bsf.close();
  }
}

```

for more infomation, please read [QuickStart.java](src/test/java/io/downgoon/bsf/example/QuickStart.java) example.


- maven dependency

``` xml
<dependency>
  <groupId>com.github.downgoon</groupId>
  <artifactId>big-sequence-file</artifactId>
  <version>0.1.0</version>
</dependency>
```

- underlying structure

when we new two '.bsf' files: ``new BigSequenceFile("hello.bsf")`` and ``new BigSequenceFile("world.bsf")``, the underlying files may look like as follows:

``` bash
$ tree .
├── hello.bsf
├── hello_0.seg
├── world.bsf
├── world_0.seg
├── world_1.seg
└── world_2.seg
```

the ``.bsf`` file manages ``meta`` info of the user namespaced ``bsf`` file (e.g. ``hello.bsf``), while multipule ``.seg`` files store ``data`` info. in general, a BSF file always consists of only one ``.bsf`` and several ``.seg`` files in underlying storage layer.


## For Developers

- [Developer Guide](docs/DeveloperGuide.md)
