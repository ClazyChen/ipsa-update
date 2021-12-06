package fpga

import chisel3._
import chisel3.util._

class HeaderInfo extends Bundle {
    val offset = UInt(const.PHV.offset_width.W)
    val length = UInt(const.PHV.offset_width.W)
}

class ParseState extends Bundle {
    val state_id = UInt(const.PAR.state_id_width.W)
    val offset   = UInt(const.PHV.offset_width.W)
    val transition_field = UInt(const.PHV.transition_field_width.W)
}

class PHV extends Bundle {
    val valid  = Bool() // whether the data is NOT bubble
    val data   = Vec(const.PHV.total_data_length, UInt(8.W))
    val header = Vec(const.max_header_number, new HeaderInfo)
    val parse  = new ParseState
    val next_processor_id  = UInt(const.processor_id_width.W)
    val next_config_id = UInt(const.config_id_width.W)
    val is_valid_processor = Bool() // whether valid in the current processor
}

class Pipeline extends Bundle {
    val ready_next = Input(Bool())
    val ready_prev = Output(Bool())
    val phv_in  = Input(new PHV)
    val phv_out = Output(new PHV) 

    def ~(next: Pipeline) = {  // directly connect this and next interface
        this.ready_next  := next.ready_prev
        next.phv_in      := this.phv_out
        next
    }
    def ~>(next: Pipeline) = { // pass the signal from this.in to next.out
        this.ready_prev  := next.ready_next
        next.phv_out     := this.phv_in
        next
    }

    def >>(inner: Pipeline) = { // embed inner interface in this (input port)
        this.ready_prev  := inner.ready_prev
        inner.phv_in     := this.phv_in
        inner
    }
    // def <<-(outer: Pipeline) = {
    //     outer.ready_prev := this.ready_prev
    //     this.phv_in      := outer.phv_in
    // }

    def <<(outer: Pipeline) = { // embed this interface in outer (output port)
        this.ready_next  := outer.ready_next
        outer.phv_out    := this.phv_out
        outer
    }
    // def >>-(inner: Pipeline) = {
    //     inner.ready_next := this.ready_next
    //     this.phv_out     := inner.phv_out
    // }
}

class PipelinePause extends Pipeline {
    val pause = Input(Bool()) // pause signal from controller
}