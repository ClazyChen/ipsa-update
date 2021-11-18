package fpga;

import scala.util.control._
import scala.math.BigInt
import scala.util.Random
import chisel3._
import chisel3.iotesters.{Driver, PeekPokeTester}

class FIFOTester(c: FIFO) extends PeekPokeTester(c) {
    def reset() {
        poke(c.io.enq.en, false)
        poke(c.io.enq.data, 0)
        poke(c.io.deq.en, false)
    }
    def enqueue(data:BigInt) {
        reset()
        println("ENQ")
        println("enqueue: " + data)
        poke(c.io.enq.en, true)
        poke(c.io.enq.data, data)
        println("enqueue - valid: " + peek(c.io.enq.valid))
        step(1)
    }
    def dequeue() {
        reset()
        println("DEQ")
        poke(c.io.deq.en, true)
        println("dequeue - valid: " + peek(c.io.deq.valid))
        step(1)
        println("dequeue: " + peek(c.io.deq.data))
    }

    for (i <- 0 until 10) {
        expect(c.io.enq.valid, i < 7)
        enqueue(i)
    }
    for (i <- 0 until 10) {
        dequeue()
        expect(c.io.deq.valid, i < 7)
        if (i < 7) {
            expect(c.io.deq.data, i)
        }
    }
}

object FIFO_TEST_OBJ extends App {
    chisel3.iotesters.Driver.execute(args, () => new FIFO(8, 64)) (
        (c) => new FIFOTester(c)
    )
}