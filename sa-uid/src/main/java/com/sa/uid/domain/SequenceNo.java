package com.sa.uid.domain;

import java.util.concurrent.atomic.AtomicLong;


public class SequenceNo {
	private Long step = 50L;
	private AtomicLong startSeq = new AtomicLong(1);
	private Long finishSeq = 0L;

	public SequenceNo(){}

	public SequenceNo(long step){
		this.step = step;
	}

	public Long next(){
		return startSeq.getAndIncrement();
	}

	public Long next(int increment){
		return startSeq.getAndAccumulate(increment, (a, b) -> a + b);
	}

	public Long getStep() {
		return step;
	}
	public void setStep(Long step){
		this.step = step;
	}
	public Long getStartSeq() {
		return startSeq.get();
	}

	public void setStartSeq(Long startSeq) {
		this.startSeq = new AtomicLong(startSeq);
	}

	public Long getFinishSeq() {
		return finishSeq;
	}

	public void setFinishSeq(Long finishSeq) {
		this.finishSeq = finishSeq;
	}
}
