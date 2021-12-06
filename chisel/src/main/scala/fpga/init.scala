package fpga

import chisel3._
import chisel3.util._


/**
    phv_in | |              | phv_out
   ------> |p|   (stage)    | ------->
           |h|   ------->   | 
ready_prev |v|   ------->   | ready_next
<--------- | |              | <-------
            \_______ pause
*/

object init {
    def pipeline(pipe: Pipeline) = {
        val phv = Reg(new PHV)
        pipe.phv_out := phv
        pipe.ready_prev := pipe.ready_next
        when (pipe.ready_next) {
            phv := pipe.phv_in
        }
        phv
    }
    def pipeline(pipe: PipelinePause) = {
        val phv = Reg(new PHV)
        pipe.phv_out := phv
        pipe.ready_prev := false.B

        val release = ~pipe.pause // not pause means release
        when (release) {
            pipe.ready_prev := true.B
            phv := pipe.phv_in
        }

        (phv, release)
    }

    def latch(signal: UInt, release: Bool) = {
        val r = Reg(UInt(signal.getWidth.W))
        when (release) {
            r := signal
        }
        r
    }
}
