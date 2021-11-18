package fpga

import chisel3._
import chisel3.util._

// a virtual stage with no actual function
// PHV[addr] = data
class VirtualStage extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline

        val addr = Input(UInt(const.PHV.offset_width.W))
        val data = Input(UInt(8.W))
    })

    val phv = init.pipeline(io.pipe)
    io.pipe.phv_out.data(addr) := data
}
