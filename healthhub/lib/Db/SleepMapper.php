<?php

declare(strict_types=1);

namespace OCA\HealthHub\Db;

use OCP\AppFramework\Db\QBMapper;
use OCP\DB\Exception;
use OCP\DB\QueryBuilder\IQueryBuilder;
use OCP\IDBConnection;

class SleepMapper extends QBMapper {

	public function __construct(IDBConnection $db) {
		parent::__construct($db, 'healthhub_sleep', SleepEntry::class);
	}

	/**
	 * @param string $userId
	 * @return SleepEntry[]
	 * @throws Exception
	 */
	public function getSleepForUser(string $userId): array {
		$qb = $this->db->getQueryBuilder();
		$qb->select('*')
			->from($this->getTableName())
			->where(
				$qb->expr()->eq('user_id', $qb->createNamedParameter($userId, IQueryBuilder::PARAM_STR))
			)
			->orderBy('date', 'ASC');

		return $this->findEntities($qb);
	}

	public function createSleepEntry(
		string $userId,
		string $date,
		float $durationHours,
		int $qualityScore
	): SleepEntry {
		$entry = new SleepEntry();
		$entry->setUserId($userId);
		$entry->setDate($date);
		$entry->setDurationHours($durationHours);
		$entry->setQualityScore($qualityScore);

		return $this->insert($entry);
	}
}
