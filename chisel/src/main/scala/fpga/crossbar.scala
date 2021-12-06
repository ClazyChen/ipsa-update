package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

class CrossbarModify extends Bundle {
    val en = Input(Bool())
    val input_id = Input(UInt(const.processor_id_width.W))
    val output_id = Input(UInt(const.processor_id_width.W))
}

/**
pipeo_in                                        pipeo_out
-----> [               PROCESSORS             ] ------->


if i->j then
            iproc(j)_out                                      (j)_in      /------ pause(j)
(last phv)   O -----> [               PROC(j)                ] -------> |phv| ----> O
      cross_level_pipe_out(i)                                                 cross_level_pipe_out(j)
where O = CROSSBAR
*/

class Crossbar extends Module {
    val io = IO(new Bundle {
        val pipe = new Pipeline
        val iproc = Vec(const.processor_number, new Pipeline)
        val mod = new CrossbarModify
    })

    val cross_level_pipe = Wire(Vec(const.processor_number, new Pipeline))
    for (j <- 0 until const.processor_number) {
        cross_level_pipe(j).ready_next := true.B
        io.iproc(j) >> cross_level_pipe(j)
    }
    cross_level_pipe.map(init.pipeline(_))

    // naive - full crossbar
    // crosspoints[j][i] = edge(i -> j) 
    val crosspoints = Reg(Vec(const.processor_number, UInt(const.processor_number.W)))

    io.pipe ~> io.iproc(0) // proc #0(locked) is the first processor in pipeline
    cross_level_pipe(const.processor_number-1) << io.pipe // proc #31(locked) is the last processor in pipeline

    for (j <- 1 until const.processor_number) {
        io.iproc(j).phv_out := 0.U.asTypeOf(new PHV)
        for (i <- 0 until const.processor_number-1) {
            when (crosspoints(j)(i)) {
                cross_level_pipe(i) << io.iproc(j)
            }
        }
    }

    when (io.mod.en) {
        val temp = Wire(Vec(const.processor_number, Bool()))
        temp(0) := false.B
        for (j <- 1 until const.processor_number) {
            temp(j) := io.mod.output_id === j.U
        }
        crosspoints(io.mod.input_id) := temp.asUInt
    }
}