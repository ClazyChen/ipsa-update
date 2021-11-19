package fpga

import chisel3._
import chisel3.util._

object connect {
    def pipeline(prev: Pipeline, next: Pipeline) = {
        prev.ready_next <> next.ready_prev
        prev.phv_out    <> next.phv_in
    }
}

object embed {
    def pipeline_in(outer: Pipeline, inner: Pipeline) = {
        outer.ready_prev <> inner.ready_prev
        outer.phv_in     <> inner.phv_in
    }
    def pipeline_out(inner: Pipeline, outer: Pipeline) = {
        inner.ready_next <> outer.ready_next
        inner.phv_out    <> outer.phv_out
    }
}