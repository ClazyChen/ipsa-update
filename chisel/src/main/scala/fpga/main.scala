package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._


object VSW_OBJ extends App {
    (new ChiselStage).execute(Array("-X", "sverilog"), Seq(new ChiselGeneratorAnnotation(() => new VirtualSwitch)))
}

object SCHD_OBJ extends App {
    (new ChiselStage).execute(Array("-X", "sverilog"), Seq(new ChiselGeneratorAnnotation(() => new Scheduler)))
}