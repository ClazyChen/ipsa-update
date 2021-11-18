package fpga

import chisel3._
import chisel3.util._

object connect {
    def pipeline(prev: Pipeline, next: Pipeline) = {
        prev.ready_next <> next.ready_prev
        prev.phv_out    <> next.phv_in
    }
}