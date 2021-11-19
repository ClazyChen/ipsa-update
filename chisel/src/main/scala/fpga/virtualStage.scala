package fpga

import chisel3._
import chisel3.util._

class VirtualStageModify extends Bundle {
    val en   = Input(Bool())
    val addr = Input(UInt(const.PHV.offset_width.W))
    val data = Input(UInt(8.W))
}

// a virtual stage with no actual function
// PHV[addr] = data
class VirtualStage extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
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
