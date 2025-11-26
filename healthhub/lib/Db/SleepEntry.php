<?php

declare(strict_types=1);

namespace OCA\HealthHub\Db;

use OCP\AppFramework\Db\Entity;

/**
 * @method string getUserId()
 * @method void setUserId(string $userId)
 * @method string getDate()
 * @method void setDate(string $date)
 * @method float getDurationHours()
 * @method void setDurationHours(float $durationHours)
 * @method int getQualityScore()
 * @method void setQualityScore(int $qualityScore)
 */
class SleepEntry extends Entity implements \JsonSerializable {

	/** @var string */
	protected $userId;

	/** @var string YYYY-MM-DD */
	protected $date;

	/** @var float */
	protected $durationHours;

	/** @var int */
	protected $qualityScore;

	public function __construct() {
		$this->addType('userId', 'string');
		$this->addType('date', 'string');
		$this->addType('durationHours', 'float');
		$this->addType('qualityScore', 'integer');
	}

	#[\ReturnTypeWillChange]
	public function jsonSerialize() {
		return [
			'id'             => $this->id,
			'user_id'        => $this->userId,
			'date'           => $this->date,
			'duration_hours' => $this->durationHours,
			'quality_score'  => $this->qualityScore,
		];
	}
}
