package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

class VirtualStageBankModify extends Bundle {
    val vs_id  = Input(UInt(const.PROC.cycle_id_width.W))
    val vs_mod = new VirtualStageModify
}

class VirtualProcessorModify extends Bundle {
    val gtw_mod = new VirtualGatewayModify
    val vsb_mod = new VirtualStageBankModify
}

// from 0 to 31 (except gateway cycle)
// set PHV[j] = processor_id
class VirtualProcessor extends Module {
    val io = IO(new Bundle {
        val pipe = new PipelinePause
        val mod  = new VirtualProcessorModify
    })

    val virtualStage = for (j <- 0 until const.PROC.cycle_number-1) yield {
        val exe = Module(new VirtualStage)
        exe.io.mod    := io.mod.vsb_mod.vs_mod
        exe.io.mod.en := io.mod.vsb_mod.vs_mod.en && io.mod.vsb_mod.vs_id === j.U
        exe
    }

    val gtw = Module(new VirtualGateway)
    gtw.io.pipe.pause := io.pipe.pause
    gtw.io.mod <> io.mod.gtw_mod

    io.pipe >> virtualStage(0).io.pipe
    for (j <- 0 until const.PROC.cycle_number-2) {
        virtualStage(j).io.pipe ~ virtualStage(j+1).io.pipe
    }
    virtualStage(const.PROC.cycle_number-2).io.pipe ~ gtw.io.pipe
    gtw.io.pipe << io.pipe
}

// object VPROC_OBJ extends App {
//     (new ChiselStage).execute(Array("-X", "sverilog"), Seq(new ChiselGeneratorAnnotation(() => new VirtualProcessor)))
// }