package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

class CrossbarModify extends Bundle {
    val en = Input(Bool())
    val input_id = Input(UInt(const.processor_id_width.W))
    val output_id = Input(UInt(const.processor_id_width.W))
    val last_proc_id = Input(UInt(const.processor_id_width.W))
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
    val crosspoints = RegInit(VecInit(Seq.fill(const.processor_number)(0.U(const.processor_number.W))))
    val last_proc = RegInit(0.U(const.processor_number.W))
    io.pipe ~> io.pipe

    for (j <- 0 until const.processor_number) {
        io.pipe ~> io.iproc(j) // all processor can be the first
        for (i <- 0 until const.processor_number-1) {
            when (crosspoints(j)(i)) {
                cross_level_pipe(i) << io.iproc(j)
            }
        }
        when (last_proc(j)) {
            cross_level_pipe(j) << io.pipe
        }
    }

    when (io.mod.en) {
        val temp = Wire(Vec(const.processor_number, Bool()))
        val temp_last = Wire(Vec(const.processor_number, Bool()))
        for (j <- 0 until const.processor_number) {
            temp(j) := io.mod.output_id === j.U
            temp_last(j) := io.mod.last_proc_id === j.U
        }
        crosspoints(io.mod.input_id) := temp.asUInt
        last_proc := temp_last.asUInt
    }
}