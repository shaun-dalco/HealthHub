<?php

declare(strict_types=1);

namespace OCA\HealthHub\Db;

use OCP\AppFramework\Db\Entity;

/**
 * @method string getUserId()
 * @method void setUserId(string $userId)
 * @method int getTimestamp()
 * @method void setTimestamp(int $timestamp)
 * @method int getBpm()
 * @method void setBpm(int $bpm)
 */
class HeartRateSample extends Entity implements \JsonSerializable {

	/** @var string */
	protected $userId;
	/** @var int */
	protected $timestamp;
	/** @var int */
	protected $bpm;

	public function __construct() {
		$this->addType('userId', 'string');
		$this->addType('timestamp', 'integer');
		$this->addType('bpm', 'integer');
	}

	#[\ReturnTypeWillChange]
	public function jsonSerialize() {
		return [
			'id' => $this->id,
			'user_id' => $this->userId,
			'timestamp' => $this->timestamp,
			'bpm' => $this->bpm,
		];
	}
}
