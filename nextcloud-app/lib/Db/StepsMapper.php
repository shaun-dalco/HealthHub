<?php

declare(strict_types=1);

namespace OCA\HealthHub\Db;

use OCP\AppFramework\Db\QBMapper;
use OCP\DB\Exception;
use OCP\DB\QueryBuilder\IQueryBuilder;
use OCP\IDBConnection;

class StepsMapper extends QBMapper {

	public function __construct(IDBConnection $db) {
		parent::__construct($db, 'healthhub_steps', StepsEntry::class);
	}

	/**
	 * @param string $userId
	 * @return StepsEntry[]
	 * @throws Exception
	 */
	public function getStepsForUser(string $userId): array {
		$qb = $this->db->getQueryBuilder();
		$qb->select('*')
			->from($this->getTableName())
			->where(
				$qb->expr()->eq('user_id', $qb->createNamedParameter($userId, IQueryBuilder::PARAM_STR))
			)
			->orderBy('date', 'ASC');

		return $this->findEntities($qb);
	}

	public function createStepsEntry(
		string $userId,
		string $date,
		int $count
	): StepsEntry {
		$entry = new StepsEntry();
		$entry->setUserId($userId);
		$entry->setDate($date);
		$entry->setCount($count);

		return $this->insert($entry);
	}
}
