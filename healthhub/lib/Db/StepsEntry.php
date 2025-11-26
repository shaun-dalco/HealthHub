<?php

declare(strict_types=1);

namespace OCA\HealthHub\Db;

use OCP\AppFramework\Db\Entity;

/**
 * @method string getUserId()
 * @method void setUserId(string $userId)
 * @method string getDate()
 * @method void setDate(string $date)
 * @method int getCount()
 * @method void setCount(int $count)
 */
class StepsEntry extends Entity implements \JsonSerializable {

	/** @var string */
	protected $userId;

	/** @var string YYYY-MM-DD */
	protected $date;

	/** @var int */
	protected $count;

	public function __construct() {
		$this->addType('userId', 'string');
		$this->addType('date', 'string');
		$this->addType('count', 'integer');
	}

	#[\ReturnTypeWillChange]
	public function jsonSerialize() {
		return [
			'id'      => $this->id,
			'user_id' => $this->userId,
			'date'    => $this->date,
			'count'   => $this->count,
		];
	}
}
