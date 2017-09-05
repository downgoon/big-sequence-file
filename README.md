# 超大规模顺序文件


## QuickStart

- 样例代码

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

详细代码请阅读 [QuickStart.java](src/test/java/io/downgoon/bsf/example/QuickStart.java)


- 文件结构

比如我们有两个BSF文件，分别是``hello.bsf``和``world.bsf``，那么存储结构形如：

``` bash
$ tree .
├── hello.bsf
├── hello_0.seg
├── world.bsf
├── world_0.seg
├── world_1.seg
└── world_2.seg
```

其中：``.bsf``存储的是BSF文件的``meta``信息；而``.seg``存储的是BSF文件的``data``信息，或叫``Segment``信息。一个完整的BSF文件，通常包含1个``.bsf``文件和多个``.seg``文件。


## 面向开发者

- [开发者指南](docs/DeveloperGuide.md)
