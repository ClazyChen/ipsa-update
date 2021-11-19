package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

class VirtualStageBankModify extends Bundle {
    val vs_id  = Input(UInt(const.PROC.cycle_id_width.W))
    val vs_mod = new VirtualStageModify
}

// from 0 to 31 (except gateway cycle)
// set PHV[j] = processor_id
class VirtualProcessor extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
        val gtw_mod = new VirtualGatewayModify
        val vsb_mod = new VirtualStageBankModify
        val id   = Input(UInt(const.processor_id_width.W))
    })

    val virtualStage = for (j <- 0 until const.PROC.cycle_number-1) yield {
        val exe = Module(new VirtualStage)
        exe.io.pipe.pause := false.B
        exe.io.mod    := io.vsb_mod.vs_mod
        exe.io.mod.en := io.vsb_mod.vs_mod.en && io.vsb_mod.vs_id === j.U
        exe
    }

    val gtw = Module(new VirtualGateway)
    gtw.io.pipe.pause <> io.pipe.pause
    gtw.io.mod <> io.gtw_mod

    embed.pipeline_in(io.pipe, virtualStage(0).io.pipe)
    for (j <- 0 until const.PROC.cycle_number-2) {
        connect.pipeline(virtualStage(j).io.pipe, virtualStage(j+1).io.pipe)
    }
    connect.pipeline(virtualStage(const.PROC.cycle_number-2).io.pipe, gtw.io.pipe)
    embed.pipeline_out(gtw.io.pipe, io.pipe)
}

// object VPROC_OBJ extends App {
//     (new ChiselStage).execute(Array("-X", "sverilog"), Seq(new ChiselGeneratorAnnotation(() => new VirtualProcessor)))
// }