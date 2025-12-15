<?php

declare(strict_types=1);

namespace OCA\HealthHub\Db;

use OCP\AppFramework\Db\QBMapper;
use OCP\DB\Exception;
use OCP\DB\QueryBuilder\IQueryBuilder;
use OCP\IDBConnection;

class WeightMapper extends QBMapper {

	public function __construct(IDBConnection $db) {
		parent::__construct($db, 'healthhub_weight', WeightEntry::class);
	}

	/**
	 * @param string $userId
	 * @return WeightEntry[]
	 * @throws Exception
	 */
	public function getWeightForUser(string $userId): array {
		$qb = $this->db->getQueryBuilder();
		$qb->select('*')
			->from($this->getTableName())
			->where(
				$qb->expr()->eq('user_id', $qb->createNamedParameter($userId, IQueryBuilder::PARAM_STR))
			)
			->orderBy('date', 'ASC');

		return $this->findEntities($qb);
	}

	public function createWeightEntry(
		string $userId,
		string $date,
		float $kg
	): WeightEntry {
		$entry = new WeightEntry();
		$entry->setUserId($userId);
		$entry->setDate($date);
		$entry->setKg($kg);

		return $this->insert($entry);
	}
}
