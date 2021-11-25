# Problem


+ *fifo.scala*

    ```scala
    class FIFOEnqueuePort(width: Int) extends Bundle {
        val en    = Input(Bool())
        val data  = Input(UInt(width.W))
        val valid = Output(Bool()) 
        // failed in Flipped, override cloneType
        override def cloneType = (new FIFOEnqueuePort(width).asInstanceOf[this.type]) //https://www.jianshu.com/p/f496cc4836ee
    }
    ```

+ *ioport.scala*

    1. set header length to 128 bytes, as 'Ethernet-VLAN-IPv6-SRv6-UDP' is longer than 64 bytes.
    2. split 1024-bit front header to pipeline and payload to FIFO.
    3. there are also some data type errors in code, `phv.data(idx)`, `io.deq_last.data(0)`

+ *init.scala*

    ```scala
    val release = ~pipe.pause && (pipe.ready_next || ~phv.valid)
    when (release) {
        pipe.ready_prev := true.B
        phv := pipe.phv_in
    }
    ```
    1. Does it also consider small bubble?  `~phv.valid`

+ *virtualStage.scala*

    1. The modification has error when `en` is `false`. see *virtualStageTest.scala*

    ```scala
    class VirtualStage extends Module {
        val io = IO(new Bundle {
            val pipe = new PipelinePause
            val mod  = new VirtualStageModify
        })

        val addr = RegInit(0.U(const.PHV.offset_width.W))
        val data = RegInit(0.U(8.W))

        val (phv, _) = init.pipeline(io.pipe)
        io.pipe.phv_out.data(addr) := data

        when (io.mod.en) {
            addr := io.mod.addr
            data := io.mod.data
        }
    }
    ```


+ ***Architecture Limitation:***
    
    1. cannot parse headers beyond front 1024 bits, and **resubmit** also cannot solve this problem; but P4 supports 4096-bit PHV, we can also split front 4096 bits to pipeline.
