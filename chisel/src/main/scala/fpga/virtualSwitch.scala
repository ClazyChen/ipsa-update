package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

/**

a virtual switch with:
core architecture (proc, xbar, sram)
front buffer & fifo & controller

NOW only IPSA arch + controller + FBUF

*/



class VirtualSwitch extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
        val mod = new VirtualArchitectureModify
        val ins = new ControlInstruction
    })

    val fbuf = Module(new FrontBuffer)
    val arch = Module(new VirtualArchitecture)
    (io.pipe >> fbuf.io.pipe) ~ arch.io.pipe << io.pipe
    //io.pipe >> arch.io.pipe << io.pipe

    val ctrl = Module(new Controller)
    ctrl.io.ins <> io.ins
    fbuf.io.pipe.pause := ctrl.io.pause_fbuf

    arch.io.pause := ctrl.io.pause
    arch.io.mod := io.mod
    arch.io.mod := ctrl.io.ctrl

}

object VSW_OBJ extends App {
    (new ChiselStage).execute(Array("-X", "sverilog"), Seq(new ChiselGeneratorAnnotation(() => new VirtualSwitch)))
}