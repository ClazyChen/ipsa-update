package fpga

import chisel3._
import chisel3.util._

class Scheduler extends Module {
    val io = IO(new Bundle {
        val pcie = new PcieBramInPort
        val ctrl = new ControlSignal
    })

    val pciei = Module(new PcieInterface)
    val ctrl  = Module(new Controller)
    val stst  = Module(new SetStage)

    pciei.io.pcie <> io.pcie
    ctrl.io.ins <> pciei.io.ctrl_ins
    stst.io.ins <> pciei.io.stst_ins
    stst.io.en <> ctrl.io.set_stage_en

    io.ctrl := ctrl.io.ctrl
    io.ctrl.mod.proc_mod.vsb_mod := stst.io.vsb_mod
}