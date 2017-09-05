# 实验

## 一个文件两份映射

### 实验目的

>一个文件，做两次内存映射，分别从两个映射中写入数据，观察是否能实时从另外一个映射中看到数据变更。

确认的确打开了两个文件句柄：

``` sql
osquery> select * from process_open_files where pid = 2230;
| 2230 | 14 | /private/var/folders/8v/jm476s_d0j7b6fbfzycrsy4w0000gn/T/mapped1609973387065791046/one-file-two-mapped.data |
| 2230 | 15 | /private/var/folders/8v/jm476s_d0j7b6fbfzycrsy4w0000gn/T/mapped1609973387065791046/one-file-two-mapped.data |
```

### 实验代码

- [OneFileTwoMapped.java](OneFileTwoMapped.java)： 同一个线程，打开两份
- [OneFileTwoMappedTwoThreads.java](OneFileTwoMappedTwoThreads.java)：在两个线程中，分别打开
- [OneFileTwoMappedProcessA.java与OneFileTwoMappedProcessB.java](OneFileTwoMappedProcessA.java)：两个进程中分别打开 （代码没有使用进程同步机制，需要DEBUG模式下设置断点，手工查看结果）

### 实验结果

| 实验名称 |  结果 |
| ----- | ----- |
| 单线程两份 | 两份数据一致 |
| 两线程两份 | 两份数据一致 |
| 两进程两份 | 两份数据 **不一样** |


## 一份映射两个线程

在 [ConcurrentPosition.java](ConcurrentPosition.java) 代码中，两个线程把``position``取值写乱了，导致到写入数据时，下标越界。

``` java
position: 8
java.nio.BufferOverflowException
	at java.nio.ByteBuffer.put(ByteBuffer.java:829)
	at java.nio.DirectByteBuffer.put(DirectByteBuffer.java:379)
	at java.nio.ByteBuffer.put(ByteBuffer.java:859)
	at io.downgoon.bsf.labs.ConcurrentPosition$2.run(ConcurrentPosition.java:83)
	at java.lang.Thread.run(Thread.java:745)
```

## 提醒

尽管``MappedByteBuffer``本身，在多个线程中，也能相互看见，但是 [BSFMeta.java](BSFMeta.java) 中，实际上隐含了内存中的冗余 [MemoMeta.java](MemoMeta.java)，导致如果打开两份，实际上数据是不一样的。
