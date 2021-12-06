package fpga

import chisel3._
import chisel3.util._
import chisel3.stage._

/**

control the INSERT / DELETE of TSPs

USE A large FIFO to describe an update operation
16 bits instruction can be divided into 2 parts
4 (opcode) + 6 (id1) + 6 (id2)
4 (opcode) + 12 (imm)

NOP   0000
PAUSE 1000 pause at the given PROC's gateway
CONT  0100 continue (delete all pause marks)
SETXB 0010 set crossbar (id1 -> id2)
SETGW 0001 set gateway (en + entry_id + proc_id + config_id)
EMIT  1111

*/

class ControlInstruction extends Bundle {
    val end = Input(Bool()) // complete the update sequence signal
    val enq = new FIFOEnqueuePort(const.CTRL.update_item_width) // update instruction
}

class ControllerPrimitive extends Bundle {
    val opcode = UInt(const.CTRL.update_opcode_width.W)
    val parameters = UInt(const.CTRL.update_parameter_width.W)

    def proc_id() = parameters(const.CTRL.update_parameter_width-1, const.CTRL.update_parameter_width-const.processor_id_width)
    def mod_para() = parameters(const.CTRL.update_parameter_width-const.processor_id_width-1, 0)
}

object ControllerPrimitiveType {
    val NOP  = 0.U(4.W)
    val PAUS = 8.U(4.W)
    val CONT = 4.U(4.W)
    val STXB = 2.U(4.W)
    val STGW = 1.U(4.W)
}

class Controller extends Module {
    val capacity = const.CTRL.update_item_capacity
    val item_width = const.CTRL.update_item_width
    val addr_width = log2Ceil(capacity)

    val io = IO(new Bundle {
        val ins = new ControlInstruction
        val pause = Vec(const.processor_number, Output(Bool()))
        val ctrl = Flipped(new VirtualArchitectureModify)
    })

    val fifo = Module(new FIFO(capacity, item_width))
    fifo.io.enq <> io.ins.enq
    fifo.io.deq.en := false.B

    val pause = RegInit(VecInit(Seq.fill(const.processor_number)(false.B)))
    io.pause := pause

    io.ctrl := 0.U.asTypeOf(new VirtualArchitectureModify)

    val sEnq :: sDeq :: sWait :: Nil = Enum(3)
    val status = RegInit(sWait)

    switch (status) {
        is (sWait) {
            when (io.ins.enq.en) {
                status := sEnq // start update
            }
        }
        is (sEnq) {
            when (io.ins.end) {
                status := sDeq // end update
            }
        }
        is (sDeq) {
            fifo.io.deq.en := true.B
            when (fifo.io.deq.valid) {
                val primitive = fifo.io.deq.data.asTypeOf(new ControllerPrimitive)
                io.ctrl.proc_id := primitive.proc_id()
                switch (primitive.opcode) {
                    is (ControllerPrimitiveType.NOP) {}
                    is (ControllerPrimitiveType.PAUS) {
                        for (j <- 0 until const.processor_number) {
                            pause(j) := j.U < primitive.parameters
                        }
                    }
                    is (ControllerPrimitiveType.CONT) {
                        pause := VecInit(Seq.fill(const.processor_number)(false.B))
                    }
                    is (ControllerPrimitiveType.STXB) {
                        io.ctrl.xbar_mod := primitive.mod_para().asTypeOf(new CrossbarModify)
                    }
                    is (ControllerPrimitiveType.STGW) {
                        io.ctrl.proc_mod.gtw_mod := primitive.mod_para().asTypeOf(new VirtualGatewayModify)
                    }
                }
            } .otherwise {
                status := sWait
            }
        }
    }
}

