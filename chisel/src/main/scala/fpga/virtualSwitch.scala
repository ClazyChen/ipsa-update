package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

/**

a virtual switch with:
core architecture (proc, xbar, sram)
front buffer & fifo & controller

NOW only IPSA arch + controller

*/



class VirtualSwitch extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
        val mod = new VirtualArchitectureModify
        val ins = new ControlInstruction
    })

    val arch = Module(new VirtualArchitecture)
    io.pipe >> arch.io.pipe << io.pipe

    val ctrl = Module(new Controller)
    ctrl.io.ins <> io.ins

    arch.io.pause := ctrl.io.pause
    arch.io.mod := io.mod
    arch.io.mod := ctrl.io.ctrl

}

object VSW_OBJ extends App {
    (new ChiselStage).execute(Array("-X", "sverilog"), Seq(new ChiselGeneratorAnnotation(() => new VirtualSwitch)))
}