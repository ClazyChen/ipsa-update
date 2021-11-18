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
        pipe.ready_prev := false.B

        when (~pipe.pause && (pipe.ready_next || ~phv.valid)) {
            pipe.ready_prev := true.B
            phv := pipe.phv_in
        }

        phv
    }
}
