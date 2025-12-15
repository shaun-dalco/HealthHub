<?php

declare(strict_types=1);

namespace OCA\HealthHub\Db;

use OCP\AppFramework\Db\Entity;

/**
 * @method string getUserId()
 * @method void setUserId(string $userId)
 * @method string getDate()
 * @method void setDate(string $date)
 * @method int getTotalSteps()
 * @method void setTotalSteps(int $totalSteps)
 * @method int getAvgHr()
 * @method void setAvgHr(int $avgHr)
 * @method int getMaxHr()
 * @method void setMaxHr(int $maxHr)
 * @method int getMinHr()
 * @method void setMinHr(int $minHr)
 * @method float getSleepHours()
 * @method void setSleepHours(float $sleepHours)
 * @method float getWeightKg()
 * @method void setWeightKg(float $weightKg)
 * @method int getUpdatedAt()
 * @method void setUpdatedAt(int $updatedAt)
 */
class Overview extends Entity implements \JsonSerializable {

	/** @var string */
	protected $userId;

	/** @var string YYYY-MM-DD */
	protected $date;

	/** @var int */
	protected $totalSteps;

	/** @var int */
	protected $avgHr;

	/** @var int */
	protected $maxHr;

	/** @var int */
	protected $minHr;

	/** @var float */
	protected $sleepHours;

	/** @var float */
	protected $weightKg;

	/** @var int unix timestamp */
	protected $updatedAt;

	public function __construct() {
		$this->addType('userId', 'string');
		$this->addType('date', 'string');
		$this->addType('totalSteps', 'integer');
		$this->addType('avgHr', 'integer');
		$this->addType('maxHr', 'integer');
		$this->addType('minHr', 'integer');
		$this->addType('sleepHours', 'float');
		$this->addType('weightKg', 'float');
		$this->addType('updatedAt', 'integer');
	}

	#[\ReturnTypeWillChange]
	public function jsonSerialize() {
		return [
			'id'           => $this->id,
			'user_id'      => $this->userId,
			'date'         => $this->date,
			'total_steps'  => $this->totalSteps,
			'avg_hr'       => $this->avgHr,
			'max_hr'       => $this->maxHr,
			'min_hr'       => $this->minHr,
			'sleep_hours'  => $this->sleepHours,
			'weight_kg'    => $this->weightKg,
			'updated_at'   => $this->updatedAt,
		];
	}
}
