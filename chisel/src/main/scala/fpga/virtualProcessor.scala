package fpga

import chisel3._
import chisel3.util._

// from 0 to 31 (except gateway cycle)
// set PHV[j] = processor_id
class VirtualProcessor extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
        val id   = Input(UInt(const.processor_id_width.W))
    })

    val virtualStage = for (j <- 0 until const.PROC.cycle_number) yield {
        val exe = Module(new VirtualStage)
        if (j == const.PROC.gateway_cycle) {
            exe.io.pipe.pause := io.pipe.pause
        } else {
            exe.io.pipe.pause := false.B
        }
        exe.io.addr := j.U
        exe.io.data := io.id
        exe
    }

    connect.pipeline(io.pipe, virtualStage(0).io.pipe)
    for (j <- 0 until const.PROC.cycle_number-1) {
        connect.pipeline(virtualStage(j).io.pipe, virtualStage(j+1).io.pipe)
    }
    connect.pipeline(virtualStage(const.PROC.cycle_number-1).io.pipe, io.pipe)

}