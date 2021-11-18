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
    val is_valid_processor = Bool() // whether valid in the current processor
}

class Pipeline extends Bundle {
    val pause = Input(Bool()) // pause signal from controller
    val ready_next = Input(Bool())
    val ready_prev = Output(Bool())
    val phv_in  = Input(new PHV)
    val phv_out = Output(new PHV) 
}
