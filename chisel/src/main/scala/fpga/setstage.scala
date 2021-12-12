package fpga

import chisel3._
import chisel3.util._

class SetStageInstruction extends Bundle {
    val enq = new FIFOEnqueuePort(const.STST.total_width)
}

class SetStage extends Module {
    val io = IO(new Bundle {
        val ins = new SetStageInstruction
        val en = Input(Bool())
        val vsb_mod = Flipped(new VirtualStageBankModify)
    })

    val fifo_vsb = Module(new FIFO(const.STST.capacity, const.STST.total_width))
    fifo_vsb.io.enq <> io.ins.enq
    fifo_vsb.io.deq.en := io.en

    io.vsb_mod := 0.U.asTypeOf(new VirtualStageBankModify)
    when (fifo_vsb.io.deq.valid) {
        io.vsb_mod := fifo_vsb.io.deq.data.asTypeOf(new VirtualStageBankModify)
    }

}