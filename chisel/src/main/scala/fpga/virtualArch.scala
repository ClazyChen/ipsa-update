package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

/**

a virtual architecture with:
crossbar, processors, SRAMs

*/

class VirtualArchitectureModify extends Bundle {
    val proc_id = Input(UInt(const.processor_id_width.W))
    val proc_mod = new VirtualProcessorModify
    val xbar_mod = new CrossbarModify
}

class VirtualArchitecture extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
        val pause = Vec(const.processor_number, Input(Bool()))
        val mod = new VirtualArchitectureModify
    })

    val xbar = Module(new Crossbar) // xbar after proc(j), pause according to proc(j+1)
    xbar.io.pause := VecInit(Cat(io.pause.asUInt(const.processor_number-2,0),false.B).asBools)
    xbar.io.mod <> io.mod.xbar_mod
    io.pipe >> xbar.io.pipe << io.pipe

    val proc =  for (j <- 0 until const.processor_number) yield {
        val exe = Module(new VirtualProcessor)
        xbar.io.iproc(j) ~ exe.io.pipe ~ xbar.io.iproc(j)
        exe.io.pipe.pause := io.pipe.pause(j)
        exe.io.mod := 0.U.asTypeOf(new VirtualProcessorModify)
        when (io.mod.proc_id === j.U) {
            exe.io.mod := io.mod.proc_mod
        }
        exe
    }
}