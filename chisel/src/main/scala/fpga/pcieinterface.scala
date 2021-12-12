package fpga

import chisel3._
import chisel3.util._

object PCIEADDR {
    val END_TAG = 0xfffff
    val CTRL_TAG = 0xffffe
}

class PcieBramInPort extends Bundle {
    val en = Input(Bool())
    val addr = Input(UInt(const.PCIE.addr_width.W))
    val data = Input(UInt(const.PCIE.data_width.W))
}

class PcieInterface extends Module {
    val io = IO(new Bundle {
        val pcie = new PcieBramInPort
        val ctrl_ins = Flipped(new ControlInstruction)
        val stst_ins = Flipped(new SetStageInstruction)
    })

    io.ctrl_ins.end := false.B
    io.ctrl_ins.enq.en := false.B
    io.ctrl_ins.enq.data := 0.U
    io.stst_ins.enq.en := false.B
    io.stst_ins.enq.data := 0.U

    when (io.pcie.en) {
        when (io.pcie.addr === PCIEADDR.END_TAG.U) {
            io.ctrl_ins.end := true.B
        } .otherwise {
            when (io.pcie.addr === PCIEADDR.CTRL_TAG.U) {
                io.ctrl_ins.enq.en := true.B
                io.ctrl_ins.enq.data := io.pcie.data
            } .otherwise {
                io.stst_ins.enq.en := true.B
                io.stst_ins.enq.data := io.pcie.data
            }
        }
    }
}