import { useState, useMemo } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  HomeIcon,
  MapIcon,
  ViewfinderCircleIcon,
  CreditCardIcon,
  ArrowRightStartOnRectangleIcon,
} from '@heroicons/react/24/solid';
import { logout } from '../../api/loginApi';
import Cookies from 'js-cookie';
import { useSnackbar } from 'notistack';
import Modal from './Modal';
import Button from './Button';

type NavItem = {
  path: string;
  icon: React.FC<React.SVGProps<SVGSVGElement>>;
  action?: string;
};

const USER_NAV_ITEMS: readonly NavItem[] = [
  { path: '/home', icon: HomeIcon },
  { path: '/map', icon: MapIcon },
  { path: '/pay', icon: ViewfinderCircleIcon },
  { path: '/history', icon: CreditCardIcon },
  { path: '#logout', icon: ArrowRightStartOnRectangleIcon, action: 'logout' },
] as const;

const OWNER_NAV_ITEMS: readonly NavItem[] = [
  { path: '/franchise/home', icon: HomeIcon },
  { path: '/map', icon: MapIcon },
  { path: '/franchise/qr', icon: ViewfinderCircleIcon },
  { path: '/franchise/income', icon: CreditCardIcon },
  { path: '#logout', icon: ArrowRightStartOnRectangleIcon, action: 'logout' },
] as const;

const BottomTab = () => {
  const role = localStorage.getItem('role');
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await logout();
    } catch {
      enqueueSnackbar('로그아웃 요청이 실패했습니다. 토큰을 초기화합니다.', {
        variant: 'warning',
      });
    } finally {
      setIsModalOpen(false);
      localStorage.clear();
      Cookies.remove('refreshToken');
      enqueueSnackbar('로그아웃 되었습니다', { variant: 'info' });
      navigate('/');
    }
  };

  const NAV_ITEMS = useMemo(
    () => (role === 'OWNER' ? OWNER_NAV_ITEMS : USER_NAV_ITEMS),
    [role],
  );
  return (
    <nav className="fixed bottom-0 z-30 flex h-14 w-full max-w-md animate-slideUp items-center justify-around gap-2 border-t-2 bg-white pb-3">
      {NAV_ITEMS.map(({ path, icon: Icon, action }) => (
        <NavLink
          key={path}
          to={action === 'logout' ? '#' : path}
          onClick={action === 'logout' ? () => setIsModalOpen(true) : undefined}
          className={({ isActive }) =>
            `flex flex-col items-center p-2 ${
              isActive && action !== 'logout' ? 'text-PRIMARY' : 'text-DARKBASE'
            }`
          }
        >
          {path === '/pay' || path === '/franchise/qr' ? (
            <>
              <div
                className="absolute -top-[38px] z-10 h-20 w-20 rounded-full border-x-2 border-t-2 bg-white"
                style={{ clipPath: 'inset(0 0 52.5% 0)' }}
              ></div>
              <div className="absolute -top-6 z-30 flex h-14 w-14 items-center justify-center rounded-full bg-PRIMARY">
                <Icon className="size-7 text-white" />
              </div>
            </>
          ) : (
            <Icon className="size-5" />
          )}
        </NavLink>
      ))}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}>
        <div className="mt-20 text-center">
          <h2 className="mb-8 text-xl">📍 로그아웃 하시겠습니까?</h2>
          <Button label="확인" onClick={handleLogout} />
        </div>
      </Modal>
    </nav>
  );
};

export default BottomTab;
